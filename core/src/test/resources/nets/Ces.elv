// Influence Diagram
//   Elvira format 

idiagram  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =181;
pos_y =198;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =138;
pos_y =415;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =408;
pos_y =303;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =991;
pos_y =194;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1126;
pos_y =635;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =689;
pos_y =273;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =899;
pos_y =511;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =606;
pos_y =745;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A C;

link B C;

link C D1;

link C U;

link D D2;

link D1 D;

link D1 D2;

link D1 U;

link D2 F;

link D2 U;

link F U;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.1 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.7 );
}

relation C A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.8 0.7 0.1 0.1 0.2 0.3 0.9 );
}

relation D D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.1 0.2 0.9 );
}

relation F D2 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.6 0.0 0.4 1.0 );
}

relation U C D1 D2 F { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 1.0 5.0 6.0 4.0 5.0 9.0 10.0 1.0 2.0 6.0 7.0 7.0 8.0 10.0 11.0 );
}

}
