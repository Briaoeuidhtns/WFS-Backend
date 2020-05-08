(ns wfs.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn])
  (:import (java.io StringReader)))

(defn recipe-by-id
  [db]
  (fn [_ {:keys [id]} _]
    (get-in db [:recipes id])))

(defn session-by-id
  [session-manager]
  (fn [_ {:keys [id]} _]
    (get-in session-manager [:sessions id])))

(defn user-by-id
  [db]
  (fn [_ {:keys [id]} _]
    (get-in db [:users id])))

(defn User->recipes
  [db]
  (fn [_ _ user] []))

(defn Session->users
  [session-manager db]
  (fn [_ _ session] []))

(defn resolver-map
  [{:keys [session-manager db]}]
  {:query/recipe-by-id (recipe-by-id db)
    :query/user-by-id (user-by-id db)
    :query/session-by-id (session-by-id session-manager)
    :User/recipes (User->recipes db)
    :Session/users (constantly [])
    :Session/recipes_connection (constantly {:page_info {:has_previous_page false
                                                         :has_next_page false
                                                         :start_cursor nil
                                                         :end_cursor nil}
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
                        (component/using [:session-manager :db]))})
