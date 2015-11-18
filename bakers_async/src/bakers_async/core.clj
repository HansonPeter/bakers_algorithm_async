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


;;Make channels
(def server-chan (chan))
(def customer-chan (chan))
(def to-calculate-chan (chan))

;;do the math, slowly.
(defn fib [n]
  (case n
    0 0
    1 1
    (+ (fib (- n 1))
       (fib (- n 2)))))


(defn do-the-math [chan]
  ;; it should be calculating the fib values and printing them
  (go
   (while "Not the heat death of the universe"
    (println "looped")
    (let
      [pair (<! chan)
       cust (first pair)
       server (second pair)]
      (println "we're able to get inside the let")
      ;;we would be using promises as a stand-in for n.
      ;(deliver server (fib cust))
      ;(println (str @server "Test"))
      (println (fib cust))
      (>! server-chan (promise))))))


(defn mixer [customer-chan server-chan]
  ;; creates pairs of customers and servers and adds them to the to-be-calculated list.
  (go
   (while "Not the heat death of the universe"
    (let [cust (<! customer-chan)
        server (<! server-chan)]
      (>! to-calculate-chan [cust server])
    ))))

;;Setup fns
(defn add-to-customers [n]
  (go (repeatedly n #(>! customer-chan (+ (rand 5) 5)))))

(defn add-to-servers [n]
  (go (repeatedly n #(>! server-chan (promise)))))

;; The main fn.
(defn -main
  "This should be pretty simple."
  []
  (print (do
    (println "You have to manually kill this thing;")
    (println "We don't use a timeout due to randomness.")
    (add-to-servers 5)
    (add-to-customers 50)
    (mixer customer-chan server-chan)
    (do-the-math to-calculate-chan)
    (fib 50) ;go returns immediately, so add some work to take time.
           )))
