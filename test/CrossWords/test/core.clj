(ns CrossWords.test.core
  (:use [CrossWords.core])
  (:use [clojure.test])
  (:use midje.sweet))

;(deftest replace-me ;; FIXME: write
;  (is false "No tests have been written."))

(facts 
	(crossable? "Foo" "Bar") => falsey
	(crossable? "Foo" "Boo") => truthy)

(fact (char-indices "Test" \t) => #{0 3})

(facts "Pairing" 
	(pairs [:a :b :c] [1 2 3]) => [:a 1 :b 2 :c 3])

(facts "Join sets"
	(join-sets "Foo" "Far") => '([#{0} #{0}])
	(join-sets "Abc" "Azc") => '([#{0} #{0}] [#{2} #{2}]))

(facts
	(valid-cross-seq? ""))