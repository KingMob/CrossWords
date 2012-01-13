(ns CrossWords.core
  (:gen-class)
  (:refer-clojure) 
  (:use 
    [clojure.set :only [intersection]]
    [clojure.math.numeric-tower :only [abs]])
  (:require 
    [clojure.math.combinatorics :as comb]
    [clojure.string :as s]))

(defn crossable? 
  [str1 str2]
  ;(println str1)
  ;(println str2)
  (if (empty? (intersection (set (.toUpperCase str1)) (set (.toUpperCase str2))))
    false
    true))

(defn char-indices 
  [str chr]
  (let
    [results #{}
    start-from 0
    char-indices-1 (fn [results start-from]
      (let
        [idx (.indexOf (.toLowerCase str) (int chr) start-from)]
			  (if (= idx -1)
			    results
			    (recur (conj results idx) (+ 1 idx)))))]
    (char-indices-1 results start-from)))

(defn pairs 
  ([s] (pairs (first s) (second s)))
  ([s1 s2]
    (for [curr-s1 s1 curr-s2 s2]
      [curr-s1 curr-s2])))

(defn join-sets 
  [str1 str2]
  (let
    [common-letters (intersection (set (.toLowerCase str1)) (set (.toLowerCase str2)))]
    (for [chr common-letters] [(char-indices str1 chr) (char-indices str2 chr)])))
    ;(for [chr common-letters] [(vec (char-indices str1 chr)) (vec (char-indices str2 chr))])))

(defn valid-cross-seq? 
  [cseq]
    (every? true? (map #(> ((nth % 1) 0) (+ ((nth % 0) 1) 1)) (partition 2 1 cseq))))

(defn end-point 
  [x y word-length direction]
  (cond
    (= direction :horiz) [(dec (+ x word-length)) y]
    (= direction :vert) [x (dec (+ y word-length))]))

(defn new-start-point 
  [initx inity word-length old-direction join-pair]
  (cond
    (= old-direction :vert) [(- initx (join-pair 1)) (+ inity (join-pair 0))]
    (= old-direction :horiz) [(+ initx (join-pair 0)) (- inity (join-pair 1))]))

(defn coord-range 
  ([cseq words] (coord-range cseq words 0 0 :horiz))
  ([cseq words initx inity direction]
    (let [word-length (count (first words))]
      (cons
        [[initx inity] (end-point initx inity word-length direction)]
        (when (seq cseq)
          (let 
            [opp-dir #(if (= % :horiz) :vert :horiz)
             [newx newy] (new-start-point initx inity word-length direction (first cseq))]
            (coord-range (next cseq) (next words) newx newy (opp-dir direction))))))))

(defn coord-list
  [cseq words]
  (let [coord-rg (coord-range cseq words)
        word-coords 
        (fn [[[stx sty] [enx eny]]]
          (for [x (range stx (inc enx)) y (range sty (inc eny))] [x y]))]
    (map word-coords coord-rg)))

(defn valid-coord-list?
  [clist]
  (let [coords (reduce concat clist)]
    (= (count coords) (+ (dec (count clist)) (count (set coords)))))) 
            
(defn min-coord 
  [dim clist]
  (case dim
    :x (apply min (take-nth 2 (flatten clist)))
    :y (apply min (take-nth 2 (next (flatten clist))))))

(defn max-coord 
  [dim clist]
  (case dim
    :x (apply max (take-nth 2 (flatten clist)))
    :y (apply max (take-nth 2 (next (flatten clist))))))

(defn normalize-coord-list
  [clist]
  (if (seq clist)
    (let [addx (abs (min-coord :x clist))
          addy (abs (min-coord :y clist))
          incfn (fn [[x y]] [(+ addx x) (+ addy y)])]
      (map #(map incfn %) clist))
    nil))

(defn write-word
  [puzzle word-clist word]
  (if (seq word-clist)
    (assoc-in (write-word puzzle (next word-clist) (next word)) (first word-clist) (first word))
    puzzle))
    
(defn write-words
  [puzzle clist words]
  (if (seq clist)
    (write-word (write-words puzzle (next clist) (next words)) (first clist) (first words))
    puzzle))
    
(defn gen-puzzle
  [clist words]
  (let [maxx (max-coord :x clist)
        maxy (max-coord :y clist)
        puzzle (vec (repeat (inc maxx) (vec (repeat (inc maxy) \space))))]
    (write-words puzzle clist words)))

  
(defn print-cross-words
  [clist words]
  (let 
    [puzzle (gen-puzzle clist words)
     transp-puzzle (apply map vector puzzle)
     str-puzzle (map #(apply str %) transp-puzzle)]
    (doall (map #(printf "%s\n" %) str-puzzle))
    (println)))

(defn mainfun [str-input]
  ;(def str-input "Cross slow words")
  ;(println str-input)
  
  (let [words (vec (.split str-input " "))
        sentence-crossable (and (> (count words) 1) (every? true? (map crossable? (drop-last words) (rest words))))]
    
    (if (not sentence-crossable)
      (do
        (printf "The sentence \"%s\" cannot be turned into a crossword.\n" str-input)
        (flush))
      (do
        ;(println "The sentence *IS* crossable.")
        (let [all-join-sets (map join-sets (drop-last words) (rest words))
              all-join-pairs (map #(vec (mapcat pairs %)) all-join-sets)
              cross-seqs (filter valid-cross-seq? (apply comb/cartesian-product all-join-pairs))
              coord-lists (map normalize-coord-list (filter valid-coord-list? 
                                                            (map coord-list cross-seqs (repeat (count cross-seqs) words))))]
          (doall (map #(print-cross-words % words) coord-lists)))))))


(defn -main [& str-input]
  (if (seq str-input)
    (mainfun (s/trim (s/join " " str-input)))
    (println "Please enter a sentence")))
