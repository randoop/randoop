function plotdata1(out)

subplot(1,3,1);
plot(out.p1,'Color','black','LineWidth',1);
title('Random walk','FontSize',14,'FontName','Arial');
xlabel('sequence length','FontSize',12,'FontName','Arial');
ylabel('generated sequences', 'FontSize',12,'FontName','Arial');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);

subplot(1,3,2);
plot(out.p2,'Color','black','LineWidth',1);
title('DRT, legal seq. pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('generated sequences', 'FontSize',12,'FontName','Arial');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);

subplot(1,3,3);
plot(out.p3,'Color','black','LineWidth',1);
title('DRT, full pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('generated sequences', 'FontSize',12,'FontName','Arial');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);
