% Removes rows corresponding to failures that
% ocurred a number of times outside the low/high range.

function [ f2, n2 ] = restrict_failures(f,n,low,high)
maxsize = 50; % fixed.
assert(low>=0 && low<=high);
assert(size(f,2)==maxsize);
assert(size(n,1)==1 && size(n,2)==size(f,1));

numfailures=f * ones(maxsize,1);
matches=(numfailures>=low & numfailures<=high);
f2 = zeros(1,maxsize);

for i=1:size(matches)
    if (matches(i))
        f2 = f2 + f(i,:);
    end
end

assert (size(matches,2)==size(n,1));
n2 = n(matches');
