/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     struct ListNode *next;
 * };
 */

struct ListNode* addTwoNumbers(struct ListNode* l1, struct ListNode* l2){
    struct ListNode *toe = NULL, *head = NULL, *n=NULL;
    struct ListNode *p1 = l1, *q2 = l2;
    int carry = 0;
    
    while ((p1 != NULL) || (q2 != NULL) || (carry != 0)) {
        n = (struct ListNode*)malloc(sizeof(struct ListNode));
        n->next = NULL;
        
        if (!toe || !head) {
            toe = n;
            head = n;
        } else {
            toe->next = n;  // move to next create node
            toe = n;    // move ptr to next create node`s head
        }
        
        if (p1 != NULL) {
            carry += p1->val;
            p1 = p1->next;
        }
        
        if (q2 != NULL) {
            carry += q2->val;
            q2 = q2->next;
        }
        
        toe->val = carry % 10;
        carry = carry/10;
    }
    
    return head;
}
