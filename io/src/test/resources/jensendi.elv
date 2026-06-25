// Influence Diagram
//   Elvira format 

idiagram  "Untitled1" { 

// Network Properties

visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Network Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =137;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("y" "n");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =258;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("y" "n");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =381;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("y" "n");
}

node T(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =262;
pos_y =180;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("y" "n");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =59;
pos_y =175;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("d11" "d21");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =257;
pos_y =286;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("d12" "d22");
}

node V1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =135;
pos_y =282;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node V2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =503;
pos_y =66;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

// links of the associated graph:

link A B;

link B C;

link B T;

link A T;

link D1 A;

link T D2;

link A V1;

link D2 V1;

link D2 C;

link C V2;

//Network Relationships: 

relation B A { 
comment = "";
deterministic=false;
values= table (0.8 0.2 0.2 0.8 );
}

relation T B A { 
comment = "";
deterministic=false;
values= table (0.9 0.5 0.5 0.1 0.1 0.5 0.5 0.9 );
}

relation A D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.8 0.8 0.2 );
}

relation V1 A D2 { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (3.0 0.0 0.0 2.0 );
}

relation C B D2 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.5 0.5 0.9 0.1 0.5 0.5 0.1 );
}

relation V2 C { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (10.0 0.0 );
}

}
