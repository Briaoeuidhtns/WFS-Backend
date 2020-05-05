(ns wfs.system
  (:require
   [com.stuartsierra.component :as component]
   [wfs.schema :as schema]
   [wfs.server :as server]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)))
