#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
@import Firebase;
@import FirebaseAuthUI;

@interface FirebaseUIAuthPlugin : CDVPlugin < FUIAuthDelegate >

- (void)initialise:(CDVInvokedUrlCommand *)command;
- (void)signIn:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;
- (void)getToken:(CDVInvokedUrlCommand *)command;

@property(strong,nonatomic) FUIAuth *authUI;
@property(strong,nonatomic) NSMutableArray<id<FUIAuthProvider>> *providers;
@property(strong,nonatomic) NSString *signInCallbackId;
@property(nonatomic) Boolean anonymous;

@end
