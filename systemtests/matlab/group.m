% Given a list of number, adds #step contiguous elements together
% and returns a list that is shorter.
function out = group(in,step)
assert(size(in,1)==1 && size(in,2)>1);

i = 1;
out = [];
while (i < size(in,2))
    upper = min(i+step-1,size(in,2));
    out = [ out sum(in(i:upper)) ];
    i = i + step;
end
