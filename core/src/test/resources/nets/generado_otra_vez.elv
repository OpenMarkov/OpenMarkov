//	   Network
//	   Elvira format

idiagram "" {

//		 Network Properties

kindofgraph = "directed";
comment = "";
default node states = ("absent" , "present");

// Variables

node A(finite-states) {
comment = "";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =154;
pos_y =126;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
comment = "";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =411;
pos_y =210;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
comment = "";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =234;
pos_y =209;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("s�" "no");
}

node U(continuous) {
comment = "";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =183;
pos_y =440;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2.0;
}

//		 Links of the associated graph:

link A B;
link A U;
link B D;
link D U;
//		Network Relationships:

relation A {
kind-of-relation = potential;
deterministic=false;
values = table(0.08 0.92 );
}

relation B A {
kind-of-relation = potential;
deterministic=false;
values = table(0.75 0.04 0.25 0.96 );
}

relation U A D {
kind-of-relation = utility;
deterministic=false;
values = table(90.0 30.0 90.0 100.0 );
}

}

