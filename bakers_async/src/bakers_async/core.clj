(ns bakers-async.core
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

;;Architecture
; servers + customer -> to-be-calculated
; customers are just a number.
; theoretically, the servers would be something like a promise or agent, but we're reducing complexity while debugging.
; right now they are just the string "server", and we don't use them


;;do the math, slowly.
(defn fib [n]
  (case n
    0 0
    1 1
    (+ (fib (- n 1))
       (fib (- n 2)))))


(defn do-math [chan]


    (let
      [pair (<! chan)
       cust (first pair)
       server (second pair)]
      (println "looped")
      (deliver server (fib cust))
      (println (str @server "Test"))
      ))

(defn do-all-things [cust-chan, serv-chan,]
  (let [to-be-calculated (chan 5)]
    (while "Not the heat death of the universe"
      (go
       (>! to-be-calculated [(<!! cust-chan) (<!! serv-chan)])
       (let
          [pair (<! to-be-calculated)
           cust (first pair)
           server (second pair)]
         (println "looped")
         (println cust)
         (deliver server (fib cust))
         (println (str @server "Test")))
       (>! serv-chan (promise))
       (print "do-all looped")))))




;;Setup fns
(defn add-to-customers [n]
  (let [cust-chan (chan n)]
    (go (doseq [_ (range n)]
          (>! cust-chan (+ (rand-int 5) 10))))
    cust-chan))


(defn add-to-servers [n]
  (let [serv-chan (chan n)]
    (go (doseq [_ (range n)]
      (>! serv-chan (promise))))
    serv-chan))


;; The main fn.
(defn -main
  "This should be pretty simple."
  []
  (print (do
    (println "You have to manually kill this thing;")
    (println "We don't use a timeout due to randomness.")
    (do-all-things (add-to-customers 50) (add-to-servers 5))
      ;go returns immediately, so add some work to take time.
           )))
