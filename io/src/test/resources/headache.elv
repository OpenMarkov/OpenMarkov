// Bayesian Network
// Elvira format 

bnet headache{ 

// Network Properties

version = 1.0;
default node states = (presente , ausente);

// Network Variables 

node node12 {
title = "Ha";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =637;
pos_y =89;
num-states = 4;
states = (no mild moderate severe);
}

node node10 {
title = "Ha-Bt";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =506;
pos_y =89;
num-states = 4;
states = (no mild moderate severe);
}

node node11 {
title = "As";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =637;
pos_y =12;
num-states = 2;
states = (present absent);
}

node node8 {
title = "Ha-Fb";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =379;
pos_y =89;
num-states = 4;
states = (no mild moderate severe);
}

node node9 {
title = "Bt";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =506;
pos_y =12;
num-states = 2;
states = (present absent);
}

node node7 {
title = "Fb";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =379;
pos_y =12;
num-states = 2;
states = (present absent);
}

node node6 {
title = "Ha-Ho";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =247;
pos_y =89;
num-states = 4;
states = (no mild moderate severe);
}

node node5 {
title = "Ho";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =247;
pos_y =11;
num-states = 2;
states = (present absent);
}

node node4 {
title = "Ha-Fe";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =121;
pos_y =89;
num-states = 4;
states = (no mild moderate severe);
}

node node2 {
title = "Ha-Ot";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =10;
pos_y =10;
num-states = 4;
states = (no mild moderate severe);
}

node node3 {
title = "Fe";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =121;
pos_y =11;
num-states = 2;
states = (present absent);
}

node node1 {
title = "ot";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =10;
pos_y =10;
num-states = 1;
states = (present);
}

// Links of the associated graph:

link node10 node12;

link node11 node12;

link node8 node10;

link node9 node10;

link node7 node8;

link node6 node8;

link node5 node6;

link node4 node6;

link node2 node4;

link node3 node4;

link node1 node2;

//Network Relationships: 

relation node12 node10 node11 {
values=table (
	1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 
	0.0 0.0 0.4 1.0 0.0 0.0 0.0 0.0  
	0.0 0.0 0.2 0.0 0.3 1.0 0.0 0.0  
	0.0 0.0 0.4 0.0 0.7 0.0 1.0 1.0  
	);
}

relation node10 node8 node9 {
values=table (
	0.3 1.0 0.0 0.0 0.0 0.0 0.0 0.0  
	0.2 0.0 0.4 1.0 0.0 0.0 0.0 0.0  
	0.2 0.0 0.2 0.0 0.3 1.0 0.0 0.0  
	0.3 0.0 0.4 0.0 0.7 0.0 1.0 1.0  
	);
}

relation node11 {
values=table (
	[present] = 0.5,
	[absent] = 0.5,
 
	);
}

relation node8 node7 node6 {
values=table (
	0.1 0.0 0.0 0.0 1.0 0.0 0.0 0.0  
	0.8 0.3 0.0 0.0 0.0 1.0 0.0 0.0  
	0.1 0.6 0.8 0.0 0.0 0.0 1.0 0.0  
	0.0 0.1 0.2 1.0 0.0 0.0 0.0 1.0  
	);
}

relation node9 {
values=table (
	[present] = 0.5,
	[absent] = 0.5,
 
	);
}

relation node7 {
values=table (
	[present] = 0.5,
	[absent] = 0.5,
 
	);
}

relation node6 node5 node4 {
values=table (
	0.0  0.0 0.0 0.0 1.0 0.0 0.0 0.0  
	0.89 0.3 0.0 0.0 0.0 1.0 0.0 0.0  
	0.11 0.6 0.0 0.0 0.0 0.0 1.0 0.0  
	0.0  0.1 1.0 1.0 0.0 0.0 0.0 1.0  
	);
}

relation node5 {
values=table (
	[present] = 0.5,
	[absent] = 0.5,
 
	);
}

relation node4 node2 node3 {
values=table (
	0.5 1.0 0.0 0.0 0.0 0.0 0.0 0.0  
	0.0 0.0 0.3 1.0 0.0 0.0 0.0 0.0  
	0.5 0.0 0.6 0.0 0.8 1.0 0.0 0.0  
	0.0 0.0 0.1 0.0 0.2 0.0 1.0 1.0  
	);
}

relation node2 node1 {
values=table (
	[no,present] = 0.93,
	[mild,present] = 0.04,
	[moderate,present] = 0.02,
	[severe,present] = 0.01,
 
	);
}

relation node3 {
values=table (
	[present] = 0.5,
	[absent] = 0.5,
 
	);
}

relation node1 {
values=table (
	[present] = 1.0,
 
	);
}

}
