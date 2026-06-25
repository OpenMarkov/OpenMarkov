// Bayesian Network
//   Elvira format 

bnet  coche { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node C21(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C19(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C18(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C16(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C14(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C13(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C12(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C11(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C10(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C8(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C7(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C6(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C5(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C22(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C23(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C24(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C25(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C26(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

node C27(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (true false);
}

// links of the associated graph:

link C27 C21;

link C13 C21;

link C19 C21;

link C26 C21;

link C18 C19;

link C16 C18;

link C24 C16;

link C14 C16;

link C11 C14;

link C4 C14;

link C10 C13;

link C7 C12;

link C4 C11;

link C8 C11;

link C3 C10;

link C14 C10;

link C4 C10;

link C4 C8;

link C5 C7;

link C6 C7;

link C6 C5;

link C5 C4;

link C4 C3;

link C1 C2;

link C3 C2;

link C8 C22;

link C12 C22;

link C11 C23;

link C22 C23;

link C23 C24;

link C19 C25;

link C6 C25;

link C16 C25;

link C25 C26;

link C26 C27;

link C19 C27;

//Network Relationships: 

relation C21 C27 C13 C19 C26 { 
values= table (0.12 0.1 0.31 0.3 0.13 0.11 0.33 0.31 0.11 0.09 0.31 0.29 0.12 0.1 0.32 0.3 0.88 0.9 0.69 0.7 0.87 0.89 0.67 0.69 0.89 0.91 0.69 0.71 0.88 0.9 0.68 0.7 );
}

relation C19 C18 { 
values= table (0.9 0.0010 0.1 0.999 );
}

relation C18 C16 { 
values= table (0.7 0.4 0.3 0.6 );
}

relation C16 C24 C14 { 
values= table (0.5 0.3 1.0E-5 1.0E-5 0.5 0.7 0.99999 0.99999 );
}

relation C14 C11 C4 { 
values= table (0.8 0.5 0.1 0.02 0.2 0.5 0.9 0.98 );
}

relation C13 C10 { 
values= table (0.3 0.05 0.7 0.95 );
}

relation C12 C7 { 
values= table (0.1 0.0020 0.9 0.998 );
}

relation C11 C4 C8 { 
values= table (0.6 0.2 0.15 0.05 0.4 0.8 0.85 0.95 );
}

relation C10 C3 C14 C4 { 
values= table (0.8 0.3 0.802 0.302 0.81 0.31 0.812 0.312 0.2 0.7 0.198 0.698 0.19 0.69 0.188 0.688 );
}

relation C8 C4 { 
values= table (0.1 0.05 0.9 0.95 );
}

relation C7 C5 C6 { 
values= table (0.8 0.7 0.7 0.2 0.2 0.3 0.3 0.8 );
}

relation C6 { 
values= table (0.7 0.3 );
}

relation C5 C6 { 
values= table (0.7 0.6 0.3 0.4 );
}

relation C4 C5 { 
values= table (0.7 0.3 0.3 0.7 );
}

relation C3 C4 { 
values= table (0.3 0.1 0.7 0.9 );
}

relation C2 C1 C3 { 
values= table (0.95 0.9 0.9 0.05 0.05 0.1 0.1 0.95 );
}

relation C1 { 
values= table (0.01 0.99 );
}

relation C22 C8 C12 { 
values= table (0.71 0.7 0.31 0.3 0.29 0.3 0.69 0.7 );
}

relation C23 C11 C22 { 
values= table (0.8 0.5 0.1 0.02 0.2 0.5 0.9 0.98 );
}

relation C24 C23 { 
values= table (0.7 0.5 0.3 0.5 );
}

relation C25 C19 C6 C16 { 
values= table (0.52 0.42 0.5 0.12 0.5 0.4 0.15 0.1 0.48 0.58 0.5 0.88 0.5 0.6 0.85 0.9 );
}

relation C26 C25 { 
values= table (0.8 0.2 0.2 0.8 );
}

relation C27 C26 C19 { 
values= table (0.8 0.5 0.82 0.51 0.2 0.5 0.18 0.49 );
}

}
