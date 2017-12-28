#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
@import Firebase;
@import FirebaseAuthUI;

@interface FirebaseUIAuthPlugin : CDVPlugin < FUIAuthDelegate >

- (void)initialise:(CDVInvokedUrlCommand *)command;
- (void)signIn:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;
- (void)getToken:(CDVInvokedUrlCommand *)command;

@property(strong) FUIAuth *authUI;
@property(strong) NSMutableArray<id<FUIAuthProvider>> *providers;

@end
