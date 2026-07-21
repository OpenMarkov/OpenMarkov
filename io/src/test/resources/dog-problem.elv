// Bayesian Network
//   Elvira format 

bnet  "Dog-Problem" { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node "light-on"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

node "bowel-problem"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

node "dog-out"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

node "hear-bark"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

node "family-out"(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = ("true" "false");
}

// links of the associated graph:

link "family-out" "light-on";

link "bowel-problem" "dog-out";

link "family-out" "dog-out";

link "dog-out" "hear-bark";

//Network Relationships: 

relation "light-on" "family-out" { 
values= table (0.6 0.05 0.4 0.95 );
}

relation "bowel-problem" { 
values= table (0.01 0.99 );
}

relation "dog-out" "bowel-problem" "family-out" { 
values= table (0.99 0.97 0.9 0.3 0.01 0.03 0.1 0.7 );
}

relation "hear-bark" "dog-out" { 
values= table (0.7 0.01 0.3 0.99 );
}

relation "family-out" { 
values= table (0.15 0.85 );
}

}
