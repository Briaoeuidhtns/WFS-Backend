(ns wfs.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [camel-snake-kebab.extras :refer [transform-keys]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.walk :refer [postwalk]]
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :as t]
   [wfs.db.query :as q]
   [wfs.util :refer [deep-merge-with]]))

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
  (fn [_ {:keys [id]}
       _]
    (q/user-by-id db id)))

(defn User->recipes
  [db]
  (fn [_ _ user]
    (q/recipes-by-user db user)))

(defn User->sessions
  [db]
  (fn [_ _ user]
    (q/sessions-by-user db user)))

(defn Session->users
  [db]
  (fn [_ _ session]
    (q/users-by-session db session)))

(defn Session->RecipesConnection
  [db]
  (fn [_ args session]
    (q/recipes-connection-by-session db (merge args session))))

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

   :query/signed (fn [_ user _] (throw+ user "Unauthorized"))})

(defn keys->gql
  [schema]
  (transform-keys #(if (#{:input-objects} %)
                     %
                     (-> %
                         name
                         (string/replace \- \_)
                         keyword))
                  schema))

(defn- edn-resource
  [name]
  (with-open [r  (io/reader (io/resource name))
              pb (java.io.PushbackReader. r)]
    (edn/read pb)))

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
