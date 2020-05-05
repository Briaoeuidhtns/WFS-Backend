(ns wfs.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn]))

(defn resolve-recipe-by-id
  [recipes-map context args value]
  (let [{:keys [id]} args]
    (recipes-map id)))

(defn resolver-map []
  [component]
  (let [dummy-data (-> (io/resource "dummy-data.edn")
                       slurp
                       edn/read-string)
        recipes-map (->> dummy-data
                         :recipes
                         (reduce #(assoc %1 (:id %2) %2) {}))]
    {:query/recipe-by-id (partial resolve-recipe-by-id recipes-map)}))

(defn load-schema []
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
  {:schema-provider (map->SchemaProvider {})})
