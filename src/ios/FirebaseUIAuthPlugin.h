#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import <GoogleSignIn/GoogleSignIn.h>

@interface FirebaseUIAuthPlugin : CDVPlugin <GIDSignInDelegate, GIDSignInUIDelegate>

- (void)initialise:(CDVInvokedUrlCommand *)command;
- (void)signIn:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;
- (void)getToken:(CDVInvokedUrlCommand *)command;

@end
