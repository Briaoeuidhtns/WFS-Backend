(ns wfs.system-test
  (:require
   [wfs.system :as sut]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.test :as t]
   [slingshot.test]
   [wfs.test.db :as db]
   [wfs.util :refer [deep-merge-with]]
   [wfs.test.util :refer [simplify]]
   [wfs.auth.user :as user]
   [com.stuartsierra.component :as component]))

(t/use-fixtures :each db/mock)

(defn- test-system
  "Creates a new system suitable for testing, and ensures that
  the HTTP port won't conflict with a default running system."
  []
  (->> (sut/new-system)
       (deep-merge-with (fn [x & _] x) db/*system* {:server {:port 8989}})))

(defn- q
  "Extracts the compiled schema and executes a query."
  ([system query & {:keys [variables context]}]
   (-> system
       (get-in [:schema-provider :schema])
       (lacinia/execute query variables context)
       simplify)))

;; Keeping all pure queries in the same test to reduce containers created
(t/deftest can-query-system
  (let [system (component/start-system (test-system))]
    (t/testing
      "user queries"
      (t/testing "can get user by id"
                 (t/is (= {:data {:user {:username "brian" :name "Brian"}}}
                          (q system "{user(id: \"brian\") {username name}}"))))

      (t/testing "can't get user by token when not authenticated"
                 (let [response (q system "{user {username name}}")]
                   (t/is (-> response
                             :data
                             :user
                             nil?)
                         "user is not returned")
                   (t/is (:errors response) ":errors is set")))

      (t/testing "can get user by token when authenticated"
                 (t/is (= {:data {:user {:username "brian" :name "Brian"}}}
                          (q system
                             "{user {username name}}"
                             :context
                             {:wfs.auth.user/identity {:username "brian"}})))
                 (t/is (= {:data {:user {:username "brian2" :name "Brian"}}}
                          (q system
                             "{user {username name}}"
                             :context
                             {:wfs.auth.user/identity {:username "brian2"}}))))

      (t/testing "can get recipes from user"
                 (t/is (= {:data {:user {:recipes [{:name "recipe 1"}
                                                   {:name "recipe 2"}
                                                   {:name "no description"}
                                                   {:name "can't edit"}]}}}
                          (q system "{user(id: \"brian\") {recipes {name}}}"))))

      (t/testing
        "can't get sessions from a user not authenticated as"
        (let [response
              (q system
                 "{user(id: \"brian\") {username sessions {id}}}")]
          (t/is (= (:data response) {:user {:username "brian" :sessions []}})
                "valid data returned")
          (t/is (:errors response)))))))

(t/deftest can-register-user
  (let [{:keys [db] :as system} (component/start-system (test-system))
        test-user               {:username "test"
                                 :password "password"
                                 :name "Test User"}]
    (t/testing
      "can register a new user"
      (t/is (thrown+? [:type ::user/does-not-exist]
                      (user/claim db test-user)))
      (let
        [reg-token
         (q
           system
           "mutation {register_user(user: {username: \"test\", password: \"password\" name: \"Test User\"})}")]
        (t/is (string? (get-in reg-token [:data :register_user])))
        (t/is (map? (user/claim db test-user)))))))
