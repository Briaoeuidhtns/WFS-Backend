(ns wfs.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn]
   [wfs.db.query :as q]
   [clojure.walk :refer [postwalk]]
   [wfs.base64 :refer [b64->str str->b64]]
   [taoensso.timbre :as t]))

(defn unqualified
  "me irl"
  [tree]
  (postwalk #(if (keyword? %) (-> % name keyword) %) tree))

(defn recipe-by-id
  [db]
  (fn [_ {:keys [id]} _]
    (unqualified (q/recipe-by-id db id))))

(defn session-by-id
  [db]
  (fn [_ {:keys [id]} _]
    (unqualified (q/session-by-id db id))))

(defn user-by-id
  [db]
  (fn [_ {:keys [id]} _]
    (unqualified (q/user-by-id db id))))

(defn User->recipes
  [db]
  (fn [_ _ user] []))

(defn User->sessions
  [db]
  (fn [_ _ user]
    (unqualified (q/session-by-user db user))))

(defn Session->users
  [db]
  (fn [_ _ session]
    []))

(defn resolver-map
  [{:keys [db]}]
  {:query/recipe-by-id (recipe-by-id db)
   :query/user-by-id (user-by-id db)
   :query/session-by-id (session-by-id db)
   :User/recipes (User->recipes db)
   :User/sessions (User->sessions db)
   :Session/users (Session->users db)
   :Session/recipes_connection (fn [_ _ _] (t/warn "recipes_connection not implemented")
                                 {:page_info {:has_previous_page false
                                              :has_next_page false
                                              :start_cursor (-> 0 str str->b64)
                                              :end_cursor (-> 1 str str->b64)}
                                  :edges []})})

(defn load-schema
  [component]
  (-> (io/resource "wfs-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

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
