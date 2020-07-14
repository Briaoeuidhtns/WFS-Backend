(ns wfs.db.system
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as jdbc]
   [honeysql.core :as sql]
   [honeysql-postgres.format]
   [taoensso.timbre :as t]))

(defrecord Database [ds config]
  component/Lifecycle
    (start [self] (assoc self :ds (jdbc/get-datasource config)))
    ;; TODO close it?
    ;; Can't tell if it needs to happen but it isn't Closeable
    (stop [self] (assoc self :db nil)))

(defn new-db
  ([] (new-db nil))
  ([config?]
   {:db (map->Database {:config (merge {:dbtype "pgsql"
                                        :dbname "wfs"
                                        :port 25432
                                        :user "postgres"
                                        :password "wfspassword"}
                                       config?)})}))

(defn sql-format
  [sql-map]
  (let [q (sql/format sql-map :namespace-as-table? true)]
    (t/info q)
    q))
