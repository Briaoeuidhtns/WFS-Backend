(ns wfs.test.db
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as q]
   [wfs.db.system :as db.system])
  (:import
   (org.testcontainers.containers PostgreSQLContainer)))

(def ^:dynamic *system* nil)

(defn mock
  [f]
  (let [c (PostgreSQLContainer.)]
    (.start c)
    (binding [*system* (component/start-system
                         (db.system/new-db
                           {:dbname (.getDatabaseName c)
                            :host (.getHost c)
                            :port (.getMappedPort
                                    c
                                    PostgreSQLContainer/POSTGRESQL_PORT)
                            :user (.getUsername c)
                            :password (.getPassword c)}))]
      (f))))
