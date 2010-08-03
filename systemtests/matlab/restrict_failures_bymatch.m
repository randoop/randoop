
function [ f2, n2 ] = restrict_failures_bymatch(f,n,match)
x = size(f,1);
assert(size(f,2)==50);
assert(size(n,2)==x);

matches=ones(size(n));
matches(strmatch(match,n)) = 0;
matches = logical(matches);
assert(size(matches,1)==1 && size(matches,2)==x);

f2 = [];
n2 = [];

for i=1:size(matches,2)
    if (matches(i))
        f2 = [ f2 ; f(i,:) ];
        n2 = [ n2 n(i) ];
    end
end

numpass = size(n,2)-size(strmatch(match,n),1);
assert(size(f2,1)==numpass && size(f2,2)==50);
assert(size(n2,1)==1 && size(n2,2)==numpass);

