(ns free-courses-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

(defn get-api-url [params]
  (let [url "https://sheets.googleapis.com/v4/spreadsheets/"]
    (str url (env :sheets-id) "/values/" params "?key=" (env :api-key))))

(defn normalize-sheets-data [sheets-data]
  (let [keys (first sheets-data)
        data (drop 1 sheets-data)]

   (map #(reduce conj {} %)
                         (map
                          (fn [values]
                            (map-indexed (fn [index value] {(nth keys index) value}) values)) data))))

(defn get-sheets-value [params]
  (normalize-sheets-data ((json/read-str ((client/get (get-api-url params)) :body) :key-fn keyword) :values)))

(defroutes app-routes
  (GET "/" []"Hello World!")
  (GET "/categories" [] (get-sheets-value "categories!A1:I"))
  (GET "/courses" [] (get-sheets-value "courses!A1:I"))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      wrap-json-body))
