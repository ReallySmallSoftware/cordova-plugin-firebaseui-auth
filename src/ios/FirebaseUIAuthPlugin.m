#import "FirebaseUIAuthPlugin.h"
@import Firebase;
@import FirebaseAuthUI;
@import FirebaseGoogleAuthUI;
@import FirebaseFacebookAuthUI;

@implementation FirebaseUIAuthPlugin

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
 
    self.authUI = [FUIAuth defaultAuthUI];
    self.authUI.delegate = self;
}

- (void)initialise:(CDVInvokedUrlCommand *)command {
    
    NSDictionary *options = [command argumentAtIndex:0 withDefault:@{} andClass:[NSDictionary class]];

    @try {
 
        [self createProviderList:options];
        
        NSString *tosUrl = [options valueForKey:@"tosUrl"];
        
        if (tosUrl != nil) {
            NSURL *url = [NSURL URLWithString:tosUrl];
            [self.authUI setTOSURL:url];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"Initialise error %@", [exception reason]);
        @throw exception;
    }
}

- (void)createProviderList:(NSDictionary *)options {
    
    self.providers = [[NSMutableArray<id<FUIAuthProvider>> alloc] init];
    
    NSArray *providers = [options valueForKey:@"providers"];
    
    if (providers != nil) {
        
        BOOL emailHidden = true;
        
        for (NSString *provider in providers) {
            if ([provider isEqualToString:@"GOOGLE"]) {
                [self.providers addObject:[[FUIGoogleAuth alloc] init]];
            }
            
            if ([provider isEqualToString:@"FACEBOOK"]) {
                [self.providers addObject:[[FUIFacebookAuth alloc] init]];
            }
            
            if ([provider isEqualToString:@"EMAIL"]) {
                emailHidden = false;
            }
        }
        
        [self.authUI setSignInWithEmailHidden:emailHidden];
        
        self.authUI.providers = self.providers;
    }
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
    
    @try {
        FIRUser *user = [[FIRAuth auth] currentUser];
    
        if (user != nil) {
            [user getIDTokenWithCompletion:^(NSString * _Nullable token, NSError * _Nullable error) {
                if (error == nil) {
                    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:token];
                
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                }
            }];
        } else {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"no_user_found"];
        
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.signInCallbackId];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"getToken error %@", [exception reason]);
        @throw exception;
    }
}

- (void)signIn:(CDVInvokedUrlCommand *)command {
    
    self.signInCallbackId = command.callbackId;
    
    @try {
        FIRUser *user = [[FIRAuth auth] currentUser];
    
        if (user != nil) {
            [self raiseEventForUser:user];
        } else {
            UINavigationController *authViewController = [self.authUI authViewController];
            [self.viewController presentViewController:authViewController animated:YES completion:nil];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"SignIn error %@", [exception reason]);
        @throw exception;
    }
}

- (BOOL)application:(UIApplication *)app
openURL:(NSURL *)url
options:(NSDictionary *)options {
    NSString *sourceApplication = options[UIApplicationOpenURLOptionsSourceApplicationKey];
    return [self.authUI handleOpenURL:url sourceApplication:sourceApplication];
}

- (void)signOut:(CDVInvokedUrlCommand *)command {
    
    @try {
        if ([self.authUI signOutWithError:nil]) {
            [self raiseEvent:@"signoutsuccess" withData:nil];
        } else {
            [self raiseEvent:@"signoutfailure" withData:nil];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
        @throw exception;
    }
}

- (void)authUI:(nonnull FUIAuth *)authUI didSignInWithUser:(nullable FIRUser *)user error:(nullable NSError *)error {
    if (error == nil) {
        [self raiseEventForUser:user];
    } else {
        
        NSDictionary *data = nil;
        
        if (error.localizedFailureReason != nil && error.localizedDescription != nil) {
            data = @{
                    @"code" : [error localizedFailureReason],
                    @"message" : [error localizedDescription]
                    };
        }
        
        [self raiseEvent:@"signinfailure" withData:data];
    }
}

- (void)raiseEventForUser:(FIRUser *)user {
    
    NSDictionary *result;
    
    NSNumber *isEmailVerified;
    
    if ([user isEmailVerified]) {
        isEmailVerified = @YES;
    } else {
        isEmailVerified = @NO;
    }

    if ([user photoURL] != nil) {
         result = @{@"email" : [user email],
                    @"emailVerified" : isEmailVerified,
                    @"name" : [user displayName],
                    @"id" : [user uid],
                    @"photoUrl" : [[user photoURL] absoluteString]
                    };
    } else {
        result = @{@"email" : [user email],
                   @"emailVerified" : isEmailVerified,
                   @"name" : [user displayName],
                   @"id" : [user uid]
                   };
    }
    
    [self raiseEvent:@"signinsuccess" withData:result];
}

- (void)raiseEvent:(NSString *)type withData:(NSDictionary *)data {
    NSDictionary *result;
    
    if (data != nil) {
        result = @{
                    @"type" : type,
                    @"data" : data
                    };
    } else {
        result = @{
                    @"type" : type
                    };
    }
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [pluginResult setKeepCallbackAsBool:YES];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.signInCallbackId];
}

@end
