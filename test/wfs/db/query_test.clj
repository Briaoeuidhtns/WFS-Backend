(ns wfs.db.query-test
  (:require
   [clojure.test :as t]
   [next.jdbc :as q]
   [wfs.db.query :as sut]
   [wfs.test.db :as db]))

(t/deftest mock-test
  "Test that the db system mocks work"
  (let [db (get-in db/*system* [:db :ds])]
    (t/is db "db was initialized")
    (t/is (= {:val 1} (q/execute-one! db ["SELECT 1 AS VAL"])))))

(t/use-fixtures :each db/mock)
