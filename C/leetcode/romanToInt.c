#define ROMI    1
#define ROMV    5
#define ROMX    10
#define ROML    50
#define ROMC    100
#define ROMD    500
#define ROMM    1000

int romanToInt(char * s){
    char prv_rom;
    int i, ans=0;
    
    for (i=0; i<strlen(s); i++) {
        switch (s[i]) {
            case 'I':
                ans += ROMI;
                prv_rom = s[i];
            break;
            case 'V':
                if (prv_rom == 'I') {
                    ans += ROMV;
                    ans -= (ROMI*2);
                } else {
                    ans += ROMV;
                }
                
                prv_rom = s[i];
            break;
            case 'X':
                if (prv_rom == 'I') {
                    ans += ROMX;
                    ans -= (ROMI*2);
                } else {
                    ans += ROMX;
                }
                
                prv_rom = s[i];
            break;
            case 'L':
                if (prv_rom == 'X') {
                    ans += ROML;
                    ans -= (ROMX*2);
                } else {
                    ans += ROML;
                }
                
                prv_rom = s[i];
            break;
            case 'C':
                if (prv_rom == 'X') {
                    ans += ROMC;
                    ans -= (ROMX*2);
                } else {
                    ans += ROMC;
                }
                
                prv_rom = s[i];
            break;
            case 'D':
                if (prv_rom == 'C') {
                    ans += ROMD;
                    ans -= (ROMC*2);
                } else {
                    ans += ROMD;
                }
                
                prv_rom = s[i];
            break;
            case 'M':
                if (prv_rom == 'C') {
                    ans += ROMM;
                    ans -= (ROMC*2);
                } else {
                    ans += ROMM;
                }
                
                prv_rom = s[i];
            break;
            default:
                break;
        }
    }
    
    return ans;
}
