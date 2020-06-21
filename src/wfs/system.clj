(ns wfs.system
  (:require
   [com.stuartsierra.component :as component]
   [wfs.schema :as schema]
   [wfs.server :as server]
   [wfs.db.system :as db]
   [wfs.subscriptions.system :as sub]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)
         (db/new-db)
         (sub/new-sub)))
