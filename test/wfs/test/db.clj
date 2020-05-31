(ns wfs.test.db
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as q]
   [wfs.db.system :as db.system]
   [clojure.string :as string])
  (:import
   (org.testcontainers.containers PostgreSQLContainer)))

(def ^:dynamic *system* nil)

(defn mock
  [f]
  (with-open
    [c (PostgreSQLContainer.
         "docker.pkg.github.com/briaoeuidhtns/wfs-backend/wfs-postgres-ci:1.1")]
    (.start c)
    (binding [*system* (db.system/new-db
                         ;; Uses the wrong driver by default, doesn't support
                         ;; setting it
                         {:jdbcUrl (string/replace-first (.getJdbcUrl c)
                                                         #"postgresql"
                                                         "pgsql")
                          :user (.getUsername c)
                          :password (.getPassword c)})]
      (f))))
