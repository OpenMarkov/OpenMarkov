//	   Bayesian Network
//	   Elvira format

bnet Grid_5_5 {

//		 Network Properties

default node states = (Presente, Ausente );

node Grid3_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =540;
//num-states = 2;
}

node Grid0_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =150;
//num-states = 2;
}

node Grid1_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =280;
//num-states = 2;
}

node Column2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =600;
pos_y =760;
//num-states = 2;
}

node Row3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1120;
pos_y =500;
//num-states = 2;
}

node Grid0_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =150;
//num-states = 2;
}

node Grid1_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =280;
//num-states = 2;
}

node Row4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1120;
pos_y =630;
//num-states = 2;
}

node Grid2_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =410;
//num-states = 2;
}

node Grid4_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =670;
//num-states = 2;
}

node Grid0_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =150;
//num-states = 2;
}

node Grid2_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =410;
//num-states = 2;
}

node Grid4_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =670;
//num-states = 2;
}

node Grid1_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =280;
//num-states = 2;
}

node Grid0_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =150;
//num-states = 2;
}

node Column0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =200;
pos_y =760;
//num-states = 2;
}

node Grid2_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =410;
//num-states = 2;
}

node Row2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1120;
pos_y =370;
//num-states = 2;
}

node Row0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1120;
pos_y =110;
//num-states = 2;
}

node Grid1_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =280;
//num-states = 2;
}

node Grid4_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =670;
//num-states = 2;
}

node Grid2_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =410;
//num-states = 2;
}

node Grid3_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =540;
//num-states = 2;
}

node Grid2_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =410;
//num-states = 2;
}

node Grid3_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =540;
//num-states = 2;
}

node Grid4_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =670;
//num-states = 2;
}

node Grid3_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =540;
//num-states = 2;
}

node Row1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1120;
pos_y =240;
//num-states = 2;
}

node Column3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =800;
pos_y =760;
//num-states = 2;
}

node Grid4_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =670;
//num-states = 2;
}

node Column4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1000;
pos_y =760;
//num-states = 2;
}

node Grid3_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =540;
//num-states = 2;
}

node Column1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =400;
pos_y =760;
//num-states = 2;
}

node Grid1_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =280;
//num-states = 2;
}

node Grid0_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =150;
//num-states = 2;
}

//		 Links of the associated graph:

link Grid0_0 Row0;
link Grid0_0 Column0;
link Grid0_1 Row0;
link Grid0_1 Column1;
link Grid0_2 Row0;
link Grid0_2 Column2;
link Grid0_3 Row0;
link Grid0_3 Column3;
link Grid0_4 Row0;
link Grid0_4 Column4;
link Grid1_0 Row1;
link Grid1_0 Column0;
link Grid1_1 Row1;
link Grid1_1 Column1;
link Grid1_2 Row1;
link Grid1_2 Column2;
link Grid1_3 Row1;
link Grid1_3 Column3;
link Grid1_4 Row1;
link Grid1_4 Column4;
link Grid2_0 Row2;
link Grid2_0 Column0;
link Grid2_1 Row2;
link Grid2_1 Column1;
link Grid2_2 Row2;
link Grid2_2 Column2;
link Grid2_3 Row2;
link Grid2_3 Column3;
link Grid2_4 Row2;
link Grid2_4 Column4;
link Grid3_0 Row3;
link Grid3_0 Column0;
link Grid3_1 Row3;
link Grid3_1 Column1;
link Grid3_2 Row3;
link Grid3_2 Column2;
link Grid3_3 Row3;
link Grid3_3 Column3;
link Grid3_4 Row3;
link Grid3_4 Column4;
link Grid4_0 Row4;
link Grid4_0 Column0;
link Grid4_1 Row4;
link Grid4_1 Column1;
link Grid4_2 Row4;
link Grid4_2 Column2;
link Grid4_3 Row4;
link Grid4_3 Column3;
link Grid4_4 Row4;
link Grid4_4 Column4;

//		Network Relationships:

relation Grid0_0 {
values = table(0.02, 0.98);
}

relation Grid0_1 {
values = table(0.53, 0.47);
}

relation Grid0_2 {
values = table(0.58, 0.42);
}

relation Grid0_3 {
values = table(0.33, 0.67);
}

relation Grid0_4 {
values = table(0.8, 0.2);
}

relation Grid1_0 {
values = table(0.8, 0.2);
}

relation Grid1_1 {
values = table(0.41, 0.59);
}

relation Grid1_2 {
values = table(0.85, 0.15);
}

relation Grid1_3 {
values = table(0.67, 0.33);
}

relation Grid1_4 {
values = table(0.13, 0.87);
}

relation Grid2_0 {
values = table(0.89, 0.11);
}

relation Grid2_1 {
values = table(0.53, 0.47);
}

relation Grid2_2 {
values = table(0.48, 0.52);
}

relation Grid2_3 {
values = table(0.22, 0.78);
}

relation Grid2_4 {
values = table(0.2, 0.8);
}

relation Grid3_0 {
values = table(0.64, 0.36);
}

relation Grid3_1 {
values = table(0.72, 0.28);
}

relation Grid3_2 {
values = table(0.53, 0.47);
}

relation Grid3_3 {
values = table(0.98, 0.02);
}

relation Grid3_4 {
values = table(0.16, 0.84);
}

relation Grid4_0 {
values = table(0.45, 0.55);
}

relation Grid4_1 {
values = table(0.35, 0.65);
}

relation Grid4_2 {
values = table(0.14, 0.86);
}

relation Grid4_3 {
values = table(0.46, 0.54);
}

relation Grid4_4 {
values = table(0.51, 0.49);
}

relation Row0 Grid0_4 Grid0_3 Grid0_2 Grid0_1 Grid0_0 {
values = table(0.57, 0.73, 0.88, 0.47, 0.29, 0.2, 0.44, 0.03, 0.98, 0.79, 0.6, 0.63, 0.15, 0.76, 0.4, 0.03, 0.53, 0.82, 0.25, 0.53, 
0.07, 0.2, 0.26, 0.14, 0.25, 0.63, 0.58, 0.36, 0.7, 0.16, 0.41, 0.97, 0.43, 0.27, 0.12, 0.53, 0.71, 0.8, 0.56, 0.97, 
0.02, 0.21, 0.4, 0.37, 0.85, 0.24, 0.6, 0.97, 0.47, 0.18, 0.75, 0.47, 0.93, 0.8, 0.74, 0.86, 0.75, 0.37, 0.42, 0.64, 
0.3, 0.84, 0.59, 0.03);
}

relation Row1 Grid1_4 Grid1_3 Grid1_2 Grid1_1 Grid1_0 {
values = table(0.28, 0.65, 0.44, 0.73, 0.4, 0.95, 0.58, 0.8, 0.14, 0.72, 0.1, 0.16, 0.51, 0.36, 0.42, 0.62, 0.38, 0.9, 0.47, 0.29, 
0.17, 0.52, 0.41, 0.82, 0.26, 0.78, 0.06, 0.19, 0.63, 0.34, 0.83, 0.41, 0.72, 0.35, 0.56, 0.27, 0.6, 0.05, 0.42, 0.2, 
0.86, 0.28, 0.9, 0.84, 0.49, 0.64, 0.58, 0.38, 0.62, 0.1, 0.53, 0.71, 0.83, 0.48, 0.59, 0.18, 0.74, 0.22, 0.94, 0.81, 
0.37, 0.66, 0.17, 0.59);
}

relation Row2 Grid2_4 Grid2_3 Grid2_2 Grid2_1 Grid2_0 {
values = table(0.75, 0.1, 0.82, 0.53, 0.65, 0.69, 0.88, 0.83, 0.87, 0.11, 0.15, 0.91, 0.99, 0.39, 0.38, 0.86, 0.28, 0.86, 0.54, 0.38, 
0.78, 0.16, 0.09, 0.81, 0.06, 0.69, 0.23, 0.97, 0.86, 0.66, 0.44, 0.98, 0.25, 0.9, 0.18, 0.47, 0.35, 0.31, 0.12, 0.17, 
0.13, 0.89, 0.85, 0.09, 0.01, 0.61, 0.62, 0.14, 0.72, 0.14, 0.46, 0.62, 0.22, 0.84, 0.91, 0.19, 0.94, 0.31, 0.77, 0.03, 
0.14, 0.34, 0.56, 0.02);
}

relation Row3 Grid3_4 Grid3_3 Grid3_2 Grid3_1 Grid3_0 {
values = table(0.11, 0.29, 0.25, 0.58, 0.92, 0.24, 0.47, 0.67, 0.37, 0.34, 0.57, 0.65, 0.49, 0.65, 0.46, 0.69, 0.18, 0.16, 0.97, 0.8, 
0.25, 0.99, 0.76, 0.65, 0.61, 0.51, 0.43, 0.27, 0.57, 0.39, 0.26, 0.27, 0.89, 0.71, 0.75, 0.42, 0.08, 0.76, 0.53, 0.33, 
0.63, 0.66, 0.43, 0.35, 0.51, 0.35, 0.54, 0.31, 0.82, 0.84, 0.03, 0.2, 0.75, 0.01, 0.24, 0.35, 0.39, 0.49, 0.57, 0.73, 
0.43, 0.61, 0.74, 0.73);
}

relation Row4 Grid4_4 Grid4_3 Grid4_2 Grid4_1 Grid4_0 {
values = table(0.43, 0.92, 0.1, 0.06, 0.04, 0.96, 0.53, 0.09, 0.69, 0.29, 0.54, 0.85, 0.95, 0.48, 0.12, 0.42, 0.75, 0.78, 0.41, 0.42, 
0.83, 0.21, 0.05, 0.78, 0.87, 0.83, 0.08, 0.35, 0.99, 0.54, 0.97, 0.16, 0.57, 0.08, 0.9, 0.94, 0.96, 0.04, 0.47, 0.91, 
0.31, 0.71, 0.46, 0.15, 0.05, 0.52, 0.88, 0.58, 0.25, 0.22, 0.59, 0.58, 0.17, 0.79, 0.95, 0.22, 0.13, 0.17, 0.92, 0.65, 
0.01, 0.46, 0.03, 0.84);
}

relation Column0 Grid4_0 Grid3_0 Grid2_0 Grid1_0 Grid0_0 {
values = table(0.94, 0.33, 0.58, 0.08, 0.38, 0.45, 0.35, 0.35, 0.35, 0.24, 0.31, 0.46, 0.07, 0.07, 0.38, 0.03, 0.76, 0.74, 0.75, 0.42, 
0.9, 0.04, 0.15, 0.77, 0.84, 0.14, 0.22, 0.76, 0.45, 0.27, 0.98, 0.39, 0.06, 0.67, 0.42, 0.92, 0.62, 0.55, 0.65, 0.65, 
0.65, 0.76, 0.69, 0.54, 0.93, 0.93, 0.62, 0.97, 0.24, 0.26, 0.25, 0.58, 0.1, 0.96, 0.85, 0.23, 0.16, 0.86, 0.78, 0.24, 
0.55, 0.73, 0.02, 0.61);
}

relation Column1 Grid4_1 Grid3_1 Grid2_1 Grid1_1 Grid0_1 {
values = table(0.59, 0.42, 0.83, 0.24, 0.22, 0.28, 0.87, 0.09, 0.34, 0.75, 0.39, 0.23, 0.47, 0.18, 0.14, 0.38, 0.55, 0.73, 0.44, 0.52, 
0.72, 0.89, 0.79, 0.86, 0.13, 0.32, 0.31, 0.4, 0.32, 0.85, 0.76, 0.44, 0.41, 0.58, 0.17, 0.76, 0.78, 0.72, 0.13, 0.91, 
0.66, 0.25, 0.61, 0.77, 0.53, 0.82, 0.86, 0.62, 0.45, 0.27, 0.56, 0.48, 0.28, 0.11, 0.21, 0.14, 0.87, 0.68, 0.69, 0.6, 
0.68, 0.15, 0.24, 0.56);
}

relation Column2 Grid4_2 Grid3_2 Grid2_2 Grid1_2 Grid0_2 {
values = table(0.67, 0.09, 0.61, 0.79, 0.09, 0.35, 0.7, 0.91, 0.05, 0.38, 0.56, 0.14, 0.87, 0.77, 0.52, 0.88, 0.52, 0.84, 0.4, 0.7, 
0.27, 0.42, 0.85, 0.99, 0.64, 0.82, 0.78, 0.41, 0.3, 0.62, 0.7, 0.98, 0.33, 0.91, 0.39, 0.21, 0.91, 0.65, 0.3, 0.09, 
0.95, 0.62, 0.44, 0.86, 0.13, 0.23, 0.48, 0.12, 0.48, 0.16, 0.6, 0.3, 0.73, 0.58, 0.15, 0.01, 0.36, 0.18, 0.22, 0.59, 
0.7, 0.38, 0.3, 0.02);
}

relation Column3 Grid4_3 Grid3_3 Grid2_3 Grid1_3 Grid0_3 {
values = table(0.67, 0.73, 0.99, 0.64, 0.76, 0.1, 0.48, 0.7, 0.26, 0.35, 0.22, 0.64, 0.92, 0.89, 0.39, 0.19, 0.12, 0.96, 0.48, 0.03, 
0.33, 0.95, 0.65, 0.78, 0.12, 0.17, 0.96, 0.97, 0.96, 0.23, 0.75, 0.63, 0.33, 0.27, 0.01, 0.36, 0.24, 0.9, 0.52, 0.3, 
0.74, 0.65, 0.78, 0.36, 0.08, 0.11, 0.61, 0.81, 0.88, 0.04, 0.52, 0.97, 0.67, 0.05, 0.35, 0.22, 0.88, 0.83, 0.04, 0.03, 
0.04, 0.77, 0.25, 0.37);
}

relation Column4 Grid4_4 Grid3_4 Grid2_4 Grid1_4 Grid0_4 {
values = table(0.56, 0.61, 0.82, 0.8, 0.07, 0.26, 0.67, 0.97, 0.07, 0.54, 0.68, 0.05, 0.52, 0.98, 0.21, 0.15, 0.52, 0.99, 0.51, 0.02, 
0.02, 0.76, 0.95, 0.95, 0.84, 0.96, 0.21, 0.41, 0.54, 0.29, 0.74, 0.31, 0.44, 0.39, 0.18, 0.2, 0.93, 0.74, 0.33, 0.03, 
0.93, 0.46, 0.32, 0.95, 0.48, 0.02, 0.79, 0.85, 0.48, 0.01, 0.49, 0.98, 0.98, 0.24, 0.05, 0.05, 0.16, 0.04, 0.79, 0.59, 
0.46, 0.71, 0.26, 0.69);
}

}

