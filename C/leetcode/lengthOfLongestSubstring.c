int lengthOfLongestSubstring(char * s){
    int cnt = 0, num,  len = strlen(s);
    char* strPtr = (char*)malloc(len);
    
    *strPtr = *s;
    
    while(1) {
        cnt++;
        
        for (num = 0; num < len; num++) {
            if (s[num] != NULL) {
                if (*strPtr == s[num]) {
                    printf("1: %c \r\n", *strPtr);
                    s[num] = NULL;
                }
            }
        }
        
        for (num = 0; num < len; num++) {
            if (s[num] != NULL) {
                *++strPtr = s[num];
                printf("2: %c \r\n", *strPtr);
                break;
            }
        }
        
        if (num == len) {
            break;
        }
    }

    return cnt;
}


