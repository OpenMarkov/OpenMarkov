// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "Untitled2" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =349;
pos_y =120;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =462;
pos_y =116;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =572;
pos_y =128;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =362;
pos_y =225;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =471;
pos_y =222;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =579;
pos_y =247;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U3(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =435;
pos_y =309;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U4(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =527;
pos_y =406;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A U;

link B U1;

link D U2;

link U U3;

link U1 U3;

link U2 U4;

link U3 U4;

//Network Relationships: 

relation A { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation B { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation U A { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 0.0 );
}

relation U1 B { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 0.0 );
}

relation U2 D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 0.0 );
}

relation U3 U U1 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation U4 U2 U3 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
