// Bayesian Network
//   Elvira format 

bnet  "Elimbel2" { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node "h"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

node "y"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "x"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "da"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "ob"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "db"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "oa"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "sb"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "sa"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = ("good" "bad" "ugly");
}

node "g"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

// links of the associated graph:

link "h" "ob";

link "h" "db";

link "y" "db";

link "x" "oa";

link "da" "sb";

link "ob" "sb";

link "db" "sa";

link "oa" "sa";

link "sb" "g";

link "sa" "g";

//Network Relationships: 

relation "h" { 
values= table (0.5 0.5 );
}

relation "y" { 
values= table (0.9 0.09 0.01 );
}

relation "x" { 
values= table (0.92 0.075 0.0050 );
}

relation "da" { 
values= table (0.25 0.3 0.45 );
}

relation "ob" "h" { 
values= table (0.5 0.2 0.3 0.2 0.2 0.6 );
}

relation "db" "h" "y" { 
values= table (0.5 0.45 0.3 0.25 0.04 0.02 0.35 0.3 0.2 0.15 0.03 0.015 0.15 0.25 0.5 0.6 0.93 0.965 );
}

relation "oa" "x" { 
values= table (0.8 0.5 0.15 0.15 0.3 0.15 0.05 0.2 0.7 );
}

relation "sb" "da" "ob" { 
values= table (0.3 0.55 0.78 0.1 0.35 0.45 0.03 0.12 0.32 0.5 0.3 0.18 0.3 0.5 0.35 0.11 0.22 0.4 0.2 0.15 0.04 0.6 0.15 0.2 0.86 0.66 0.28 );
}

relation "sa" "db" "oa" { 
values= table (0.4 0.75 0.88 0.17 0.42 0.52 0.07 0.17 0.38 0.48 0.2 0.07 0.37 0.47 0.34 0.19 0.3 0.38 0.12 0.05 0.05 0.46 0.11 0.14 0.74 0.53 0.24 );
}

relation "g" "sb" "sa" { 
values= table (0.5 0.35 0.15 0.35 0.5 0.35 0.15 0.35 0.5 0.5 0.65 0.85 0.65 0.5 0.65 0.85 0.65 0.5 );
}

}
