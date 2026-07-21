// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =229;
pos_y =207;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =341;
pos_y =126;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =403;
pos_y =214;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =408;
pos_y =314;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =559;
pos_y =316;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U2(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =486;
pos_y =414;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U3(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =248;
pos_y =403;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U4(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =366;
pos_y =503;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A B;

link A D;

link A U3;

link B D;

link B U;

link B U1;

link B U3;

link D U;

link D U1;

link U U2;

link U1 U2;

link U2 U4;

link U3 U4;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.07 0.93 );
}

relation U3 A B { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (2.0 1.0 3.0 0.0 );
}

relation U B D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (70.0 50.0 60.0 80.0 );
}

relation U2 U1 U { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation U1 D B { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (60.0 90.0 10.0 20.0 );
}

relation U4 U3 U2 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.75 0.3 0.25 0.7 );
}

}

