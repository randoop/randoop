/*
 * parallel GB check
 * $Id: pgb.spin 187 2004-01-25 14:43:31Z kredel $
 */

byte idler = 0;
#define PROCNUM 3
#define noJobs (idler == PROCNUM)

byte pairsRemaining = 10;
#define nextPair (pairsRemaining != 0)
byte maxPairs = 30;

#define erledigt ( (! nextPair) && noJobs )

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

proctype Server () {
 do
 :: idler++;
    if 
    :: ( erledigt ) -> break; //goto fertig;
    :: ( ! nextPair ) -> skip //delay;
    :: else skip;
    fi;
    idler--;
    getPair();
    /* compute H-Pol */
 progress: skip;
    addPair();
 od;
 fertig: assert( ! nextPair );
}

active proctype Monitor() {
  atomic {
    noJobs -> ! nextPair;
  }
}

init {
  idler = 0; 
  run Server();
  run Server();
  run Server();
}
