(ns wfs.base64
  (:import
   java.util.Base64))

(defn str->b64
  [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn b64->str
  [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))
