function w = work(s)
assert(size(s,1)==1 && size(s,2)>1);
w = zeros(size(s));
for i=1:size(s,2)
    w(1,1:i) = w(1,1:i) + s(1,i);
end
