(ns wfs.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [camel-snake-kebab.extras :refer [transform-keys]]
   [clojure.string :as string]
   [clojure.walk :refer [postwalk]]
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.resolve :as resolve]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :as t]
   [wfs.db.query :as q]
   [wfs.auth.user :as user]
   [wfs.db.mutations :as mut]
   [wfs.util :refer [deep-merge-with edn-resource]]))

(defn unqualified
  "me irl"
  [tree]
  (postwalk #(if (keyword? %)
               (-> %
                   name
                   keyword)
               %)
            tree))

(defn recipe-by-id
  [db]
  (fn [_ {:keys [id]}
       _]
    (q/recipe-by-id db id)))

(defn session-by-id
  [db]
  (fn [_ {:keys [id]}
       _]
    (q/session-by-id db id)))

(defn user-by-id
  [db]
  (fn [{{auth-id :username} ::user/identity} {arg-id :id} _]
    (if-let [id (or arg-id auth-id)]
      (q/user-by-id db id)
      (resolve/with-error nil
                          {:message
                             "No :id and no valid authentication found"}))))

(defn User->recipes
  [db]
  (fn [_ _ user]
    (q/recipes-by-user db user)))

(defn User->sessions
  [db]
  (fn [{{auth-name :username} ::user/identity} _ {req-name :username :as user}]
    (if (and auth-name (= auth-name req-name))
      (q/sessions-by-user db user)
      (resolve/with-error ()
                          {:message "You can only access your own sessions"
                           ::required req-name
                           ::authorized-for auth-name}))))

(defn Session->users
  [db]
  (fn [_ _ session]
    (q/users-by-session db session)))

(defn Session->RecipesConnection
  [db]
  (fn [_ args session]
    (q/recipes-connection-by-session db (merge args session))))

(defn signed
  [db]
  (fn [_ {:keys [claim]}
       _]
    (user/token db claim)))

(defn register-user
  [db]
  (fn [_ {:keys [user]}
       _]
    (mut/add-user db (user/new user))
    ;; Round trip to be sure account is valid
    (user/token db user)))

(defn start-session
  [db]
  (fn [_ {:keys []}
       _]
    (mut/start-session db)))

(defn resolver-map
  [{:keys [db]}]
  {:query/recipe-by-id (recipe-by-id db)
   :query/user-by-id (user-by-id db)
   :query/session-by-id (session-by-id db)
   :User/recipes (User->recipes db)
   :User/sessions (User->sessions db)
   :Session/users (Session->users db)
   :Session/recipes-connection (Session->RecipesConnection db)
   :mutation/yoink-recipe (constantly nil)
   :mutation/rate-recipe (constantly nil)
   :mutation/start-session (start-session db)

   :query/signed (signed db)
   :mutation/register-user (register-user db)})

(defn keys->gql
  [schema]
  (transform-keys #(if (#{:input-objects} %)
                     %
                     (-> %
                         name
                         (string/replace \- \_)
                         keyword))
                  schema))

(defn load-schema
  [component]
  (let [slices  (map edn-resource '("wfs-schema.edn" "auth-schema.edn"))
        combine (partial deep-merge-with
                         (fn [& vals]
                           (throw+ {:type ::duplicate-keys :keys vals})
                           "Duplicate keys in schema"))
        schema  (apply combine slices)]
    (-> schema
        keys->gql
        (util/attach-resolvers (resolver-map component))
        schema/compile)))

(defrecord SchemaProvider [schema]

  component/Lifecycle

    (start [self]
      (assoc self :schema (load-schema self)))

    (stop [self]
      (assoc self :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:db]))})
