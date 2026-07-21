// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node Disease_X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =109;
pos_y =40;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Result_Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =288;
pos_y =132;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("not performed" "positive" "negative");
}

node Test_T(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =462;
pos_y =37;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Treatment_D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =284;
pos_y =241;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Global_utility_U0(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =277;
pos_y =418;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link Disease_X Global_utility_U0;

link Disease_X Result_Y;

link Result_Y Treatment_D;

link Test_T Global_utility_U0;

link Test_T Result_Y;

link Treatment_D Global_utility_U0;

//Network Relationships: 

relation Disease_X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.07 0.93 );
}

relation Result_Y Disease_X Test_T { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.0 1.0 0.0 1.0 0.91 0.0 0.03 0.0 0.09 0.0 0.97 0.0 );
}

relation Global_utility_U0 Disease_X Test_T Treatment_D { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (50.0 0.0 52.0 2.0 60.0 70.0 62.0 72.0 );
}

}
