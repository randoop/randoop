function a = average(n)
assert(size(n,1)==1 && size(n,2)>1);

p1 = []; p2 = []; p3 = []; p4 = []; p5 = []; 
p6 = []; p7 = []; p8 = []; p9 = [];

for i=1:size(n,2)
    ni = n(1,i);
    p1 = [ p1 ; ni.p1 ];
    p2 = [ p2 ; ni.p2 ];
    p3 = [ p3 ; ni.p3 ];
    p4 = [ p4 ; ni.p4 ];
    p5 = [ p5 ; ni.p5 ];
    p6 = [ p6 ; ni.p6 ];
    p7 = [ p7 ; ni.p7 ];
    p8 = [ p8 ; ni.p8 ];
    p9 = [ p9 ; ni.p9 ];
end

a.p1 = mean(p1);
a.p2 = mean(p2);
a.p3 = mean(p3);
a.p4 = mean(p4);
a.p5 = mean(p5);
a.p6 = mean(p6);
a.p7 = mean(p7);
a.p8 = mean(p8);
a.p9 = mean(p9);
a.x = n(1).x;