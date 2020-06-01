(ns wfs.db.mutations-test
  (:require
   [wfs.db.mutations :as sut]
   [clojure.test :as t]
   [next.jdbc :as q]
   [wfs.test.db :as db]
   [com.stuartsierra.component :as component]))

(t/use-fixtures :each db/mock)

(t/deftest add-user-test
  "Test that a user can be added"
  (let [db          (:db (component/start-system db/*system*))
        n-user-q    ["SELECT COUNT(*) FROM registered_user"]
        init-n-user (:count (q/execute-one! (:ds db) n-user-q))
        test-user   {:name "Test User"
                     :username "testuser"
                     :password "imagine this is hashed"}]
    (t/is (sut/add-user db test-user) "a user was successfully added")
    (t/is (= (inc init-n-user) (:count (q/execute-one! (:ds db) n-user-q)))
          "The database has one more user")
    (t/is
      (=
        1
        (:exists?
          (q/execute-one!
            (:ds db)
            ["SELECT COUNT(*) AS \"exists?\" FROM registered_user AS r WHERE r.username = ?"
             (:username test-user)]))))))
