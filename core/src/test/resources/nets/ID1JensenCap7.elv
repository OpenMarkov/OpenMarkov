// Influence Diagram
//   Elvira format 

idiagram  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =107;
pos_y =236;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =461;
pos_y =383;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =169;
pos_y =72;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =594;
pos_y =73;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =420;
pos_y =219;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =734;
pos_y =210;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =436;
pos_y =575;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A B;

link A C;

link A U;

link B C;

link B E;

link C D2;

link D1 A;

link D2 E;

link D2 U;

link E U;

//Network Relationships: 

relation A D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.5 0.8 0.5 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.2 0.9 0.8 );
}

relation C A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.3 0.8 0.9 0.9 0.7 0.2 0.1 );
}

relation E B D2 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.2 0.3 0.8 0.1 0.8 0.7 0.2 );
}

relation U A D2 E { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 );
}

}
