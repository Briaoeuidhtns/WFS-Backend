(ns wfs.auth.interceptor
  (:require
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :as middleware]
   [io.pedestal.interceptor.helpers :refer [defbefore]]
   [taoensso.timbre :as log]
   [wfs.auth.user :as auth]))

(def ^:private auth-backend
  (jws-backend
    {:secret auth/pubkey :token-name "Bearer" :options {:alg :eddsa}}))

(defbefore
  user-info
  [{:keys [request] :as context}]
  (let [ident (middleware/authenticate-request request [auth-backend])]
    (log/info "Claim validated as" identity)
    (assoc-in context
      [:request :lacinia-app-context ::auth/identity]
      ident)))
