/*
 * Distributed GB communications check
 * $Id: dgb.spin 187 2004-01-25 14:43:31Z kredel $
 */

mtype = { Get, Fin, Pair, Hpol };

byte idler = 0;
#define PROCNUM 3
#define noJobs (idler == PROCNUM)

byte pairsRemaining = 10;
#define nextPair (pairsRemaining != 0)
byte maxPairs = 50;

#define erledigt (! nextPair && noJobs )

inline addPair() {
 atomic { 
   if 
   :: ( maxPairs == 0 ) -> skip;
   :: ( maxPairs > 0 ) -> pairsRemaining++; maxPairs--; 
   fi
 }
}

inline getPair() {
 atomic {
   if
   :: (pairsRemaining == 0) -> skip;
   :: (pairsRemaining > 0)  -> pairsRemaining--; 
   fi
 }
}

chan pairchan[PROCNUM] = [2] of { mtype };

proctype Server (chan pairs) {
 do
 :: idler++; pairs ? Get; 
    if 
    :: ( erledigt ) -> pairs ! Fin; break; //goto fertig; 
    :: ( ! nextPair ) -> skip //delay;
    :: else skip;
    fi;
    idler--;
    getPair();
    pairs ! Pair;
    /* compute H-Pol in client */
 progress: skip;
    pairs ? Hpol;
    addPair();
 od;
 fertig: assert( ! nextPair );
}

proctype Client (chan pairs) {
 do 
 :: pairs ! Get;
    if 
    :: pairs ? Fin  -> break;
    :: pairs ? Pair -> pairs ! Hpol;
    fi
 od
}

active proctype Monitor() {
  atomic {
    noJobs -> ! nextPair;
  }
}

init {
  idler = 0; 
  run Server(pairchan[0]);
  run Server(pairchan[1]);
  run Client(pairchan[0]);
  run Client(pairchan[1]);
  run Client(pairchan[2]);
  run Server(pairchan[2]);
}
