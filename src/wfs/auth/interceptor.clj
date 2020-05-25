(ns wfs.auth.interceptor
  (:require
   [io.pedestal.interceptor.helpers :refer [defbefore]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :as middleware]
   [wfs.auth.user :as auth]))

(def auth-backend
  (jws-backend
    {:secret auth/pubkey :token-name "Bearer" :options {:alg :eddsa}}))

(defbefore
  user-info
  [context]
  (update context :request middleware/authentication-request auth-backend))
