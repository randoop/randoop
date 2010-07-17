function printfnames(f,n)
assert(size(n,1)==1 && size(n,2)>1);
assert(size(f,1)==size(n,2) && size(f,2)==50);

for i=1:size(n,2)
    [s, err ] = sprintf('%d %s', sum(f(i,:)), n{1,i});
    disp(s);
end