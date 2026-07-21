//	   Network
//	   Elvira format

idiagram "" {

//		 Network Properties

kindofgraph = "directed";
visualprecision = "0.0";
version = 1.0;
default node states = ("present" , "absent");

// Variables

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =328;
pos_y =142;
relevance = 7.0;
purpose = "";
num-states = 2
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =642;
pos_y =142;
relevance = 7.0;
purpose = "";
num-states = 2
states = ("present" "absent");
}

node T(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =492;
pos_y =315;
relevance = 7.0;
purpose = "";
num-states = 2
states = ("present" "absent");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =643;
pos_y =467;
relevance = 7.0;
purpose = "";
num-states = 2
states = ("yes" "no");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =145;
pos_y =317;
relevance = 7.0;
purpose = "";
num-states = 3
states = ("maybe" "yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =322;
pos_y =465;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

//		 Links of the associated graph:

link A B;

link A T;

link A U;

link B T;

link T D2;

link D2 U;

link D1 A;

//		Network Relationships:

relation A D1 {
kind-of-relation = potential;
deterministic=false;
values = table(0.5 0.6 0.3 0.5 0.4 0.7 );
}

relation B A {
kind-of-relation = potential;
deterministic=false;
values = table(0.3 0.95 0.7 0.05 );
}

relation T A B {
kind-of-relation = potential;
deterministic=false;
values = table(0.1 0.2 0.3 0.4 0.9 0.8 0.7 0.6 );
}

relation U A D2 {
kind-of-relation = utility;
deterministic=false;
values = table(2.0 1.0 9.0 10.0 );
}

}

