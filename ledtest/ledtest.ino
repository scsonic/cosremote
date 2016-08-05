#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <stdarg.h>
#include <Servo.h>

#define BITERATE 115200 
#define ALLLED 255
#define SKIP 254
#define SHIFT 253 // moveing led x step

#define COMMAND 252
#define CMD_PIXEL_COUNT 1
#define CMD_BRITENESS 2
#define CMD_DEBUG 3 


#define HAND_COMMAND 251
#define ALL_FINGER 255
#define ALL_SERVO 254
// pin DO NOT USE PIN3 FOR INPACTABLE WITH LED XD

Servo inmoov[6] ; // arm = 0, thumb=5 


#define MAX_PIXEL_COUNT 64 

boolean debug = true ;
int ardprintf(char *str, ...) ;

// define skip bit = 254 damn it @@
// 250~255 = special bit
int pin = 3 ;
int PIXEL_COUNT = MAX_PIXEL_COUNT ;
int brightness = 200 ;
boolean isChanged = true ;
boolean byteMode = false ;

#define SERIAL Serial
char line[100] ;

Adafruit_NeoPixel strip = Adafruit_NeoPixel(PIXEL_COUNT, pin, NEO_GRB + NEO_KHZ800);

void setup() {
  
    strip.begin();
    //strip.show(); // Initialize all pixels to 'off'
    SERIAL.begin(BITERATE) ;
    
    int i = 0 ; 
    for ( i = 0 ; i < PIXEL_COUNT ; i++ )
    {
         strip.setPixelColor( i, strip.Color(i*2,i*2,i*2) ) ;
    }
    strip.setBrightness( brightness ) ;
    strip.show() ;
    
    inmoov[0].attach(A0); 
    inmoov[1].attach(A1);
    inmoov[2].attach(A2);
    inmoov[3].attach(A3);
    inmoov[4].attach(4);
    inmoov[5].attach(5);    
}

int wait = 20 ;
int notShowCount = 0 ;
int  r,g,b, cmd, data ;
int shift, at ;
uint32_t c ;
void loop() 
{
  int led;
  
  if ( SERIAL.available() > 0 ) {
      led = SERIAL.read() ;
      while ( led == SKIP ) { // skip the byte to align ... @@
          show();
          led = SERIAL.read() ;
      }  
      
      if ( led >= 0 && led <= 250 ) {
          isLed( led ) ;
      }
      else if ( led == ALLLED ) {
        isLed( led ) ;
      }
      else if ( led == SHIFT ) {
         isShift( led ) ;
      }
      else if ( led == COMMAND ) {
            while (SERIAL.available() < 1) { } ;
            
            cmd = SERIAL.read() ;
            if ( cmd == CMD_PIXEL_COUNT ) {
              while (SERIAL.available() < 1) { } ;
              PIXEL_COUNT = SERIAL.read() ;
              changePixelCount( PIXEL_COUNT) ;
            }
            else if (cmd == CMD_BRITENESS ) {
              while (SERIAL.available() < 1) { } ;
              brightness = SERIAL.read() ;
              strip.setBrightness( brightness ) ;
              strip.show() ;
            }
            else if ( cmd == CMD_DEBUG ) { 
              while (SERIAL.available() < 1) { } ;
              debug = SERIAL.read() ;
            }
            
            
        }
        else if ( led == HAND_COMMAND ) {
           hand_command() ; 
        }
  }
  else  {
    show() ;
  }
  
}

void show() {
   if ( isChanged == true) {
    isChanged = false ;
    strip.show() ;
  }
}

void isLed(int led) {
      while ( SERIAL.available()  < 3 ) { } ;
       r = SERIAL.read() ;
      g = SERIAL.read() ;
      b = SERIAL.read() ;
      
      ardprintf("%d@%d,%d,%d\n", led, r,g,b ) ;
      //SERIAL.print( line ) ;
      
      if ( led == ALLLED ) // for all leds 
      {
        for ( int i = 0 ; i < PIXEL_COUNT ; i++ ) {
          strip.setPixelColor( i, strip.Color(r,g,b) ) ;
        }
      }
      else if ( led >= 0 && led < PIXEL_COUNT ) {
          strip.setPixelColor( led, strip.Color(r,g,b) ) ;
      }
      
      isChanged = true ;
}

void isShift( int led ) {
       while ( SERIAL.available()  < 1 ) { } ;
       shift = SERIAL.read() ;
       ardprintf("Shift %d", shift) ;
//       SERIAL.println( shift ) ;
       c = strip.getPixelColor( 0 ) ;
       for ( int i = 0 ; i < PIXEL_COUNT ; i++ ) {
         at = (i+shift) % PIXEL_COUNT ;
         strip.setPixelColor( i, strip.getPixelColor( at ) ) ;
       }
       strip.setPixelColor( PIXEL_COUNT -1, c ) ;
       
       
       isChanged = true ;
}

void changePixelCount(int c) {
    for ( int i = 0 ; i < MAX_PIXEL_COUNT ; i++ ) {
        strip.setPixelColor( i, strip.Color( 0,0,0 ) ) ;
    }
    strip.show() ;
}



void hand_command() {
  while (SERIAL.available() < 2) { } ;
  
  int index = Serial.read() ;
  int degree = Serial.read() ;
  
  if ( index == ALL_FINGER ) {
    while (SERIAL.available() < 1) { } ;

    inmoov[1].write( degree ) ;
    inmoov[2].write( degree ) ;
    inmoov[3].write( degree ) ;
    inmoov[4].write( degree ) ;
    inmoov[5].write( degree ) ;
  }
  else if ( index == ALL_SERVO ) {
    inmoov[0].write( degree ) ;
    inmoov[1].write( degree ) ;
    inmoov[2].write( degree ) ;
    inmoov[3].write( degree ) ;
    inmoov[4].write( degree ) ;
    inmoov[5].write( degree ) ;
  }
  else if ( index >= 0 && index <= 5 ) {
     // each servo    
    inmoov[index].write( degree ) ;
  }
  else {
    Serial.print("led pin error @@");
  }
}
// ======================================================================================================================

/* 
    This code should be pasted within the files where this function is needed.
    This function will not create any code conflicts.
    
    The function call is similar to printf: ardprintf("Test %d %s", 25, "string");
    To print the '%' character, use '%%'
    This code was first posted on http://arduino.stackexchange.com/a/201
*/

//#ifndef ARDPRINTF
//#define ARDPRINTF
#define ARDBUFFER 16 //Buffer for storing intermediate strings. Performance may vary depending on size.


int ardprintf(char *str, ...) //Variadic Function
{
  if ( ! debug ) { return 0 ; } 
  
  int i, count=0, j=0, flag=0;
  char temp[ARDBUFFER+1];
  for(i=0; str[i]!='\0';i++)  if(str[i]=='%')  count++; //Evaluate number of arguments required to be printed
  
  va_list argv;
  va_start(argv, count);
  for(i=0,j=0; str[i]!='\0';i++) //Iterate over formatting string
  {
    if(str[i]=='%')
    {
      //Clear buffer
      temp[j] = '\0'; 
      Serial.print(temp);
      j=0;
      temp[0] = '\0';
      
      //Process argument
      switch(str[++i])
      {
        case 'd': Serial.print(va_arg(argv, int));
                  break;
        case 'l': Serial.print(va_arg(argv, long));
                  break;
        case 'f': Serial.print(va_arg(argv, double));
                  break;
        case 'c': Serial.print((char)va_arg(argv, int));
                  break;
        case 's': Serial.print(va_arg(argv, char *));
                  break;
        default:  ;
      };
    }
    else 
    {
      //Add to buffer
      temp[j] = str[i];
      j = (j+1)%ARDBUFFER;
      if(j==0)  //If buffer is full, empty buffer.
      {
        temp[ARDBUFFER] = '\0';
        Serial.print(temp);
        temp[0]='\0';
      }
    }
  };
  
  Serial.println(); //Print trailing newline
  return count + 1; //Return number of arguments detected
}

//#undef ARDBUFFER
//#endif
