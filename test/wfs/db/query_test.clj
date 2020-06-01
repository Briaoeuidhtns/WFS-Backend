(ns wfs.db.query-test
  (:require
   [clojure.test :as t]
   [next.jdbc :as q]
   [wfs.db.query :as sut]
   [wfs.test.db :as db]
   [com.stuartsierra.component :as component]))

;; Can be run only once since these should all be pure
(t/use-fixtures :once db/mock)

(t/deftest mock-test
  "Test that the db system mocks work"
  (let [db (get-in (component/start-system db/*system*) [:db :ds])]
    (t/is db "db was initialized")

    (t/is (= {:val 1}
             (q/execute-one! db ["SELECT 1 AS VAL"]))
          "trivial query valid")

    (t/is (= "test"
             (:db
               (q/execute-one!
                 db
                 ["SELECT current_database() AS db"])))
          "connected to correct db")

    (t/is (= "recipe"
             (:name?
               (q/execute-one!
                 db
                 ["SELECT CAST (to_regclass('recipe') AS TEXT) as \"name?\""])))
          "schema was loaded")))
