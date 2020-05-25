#import "FirebaseUIAuthPlugin.h"
@import Firebase;
@import FirebaseUI;

@implementation FirebaseUIAuthPlugin

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.authUI = [FUIAuth defaultAuthUI];
    self.authUI.delegate = self;
    self.anonymous = false;
}

- (void)initialise:(CDVInvokedUrlCommand *)command {

    NSDictionary *options = [command argumentAtIndex:0 withDefault:@{} andClass:[NSDictionary class]];

    @try {
        self.signInCallbackId = command.callbackId;

        [self createProviderList:options];

        NSString *tosUrl = [options valueForKey:@"tosUrl"];

        if (tosUrl != nil) {
            NSURL *url = [NSURL URLWithString:tosUrl];
            [self.authUI setTOSURL:url];
        }

        NSNumber *anonymous = [options valueForKey:@"anonymous"];

        if ([anonymous isEqualToNumber:[NSNumber numberWithBool:YES]]) {
            self.anonymous = true;
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

    NSNumber *iOSDisable = [options valueForKey:@"iOSDisable"];

    if (providers != nil) {

        for (NSString *provider in providers) {
            if (@available(iOS 11.0, *)) {
                if (![iOSDisable isEqualToNumber:[NSNumber numberWithBool:YES]]) {
                    if ([provider isEqualToString:@"GOOGLE"]) {
                        [self.providers addObject:[[FUIGoogleAuth alloc] init]];
                    }

                    if ([provider isEqualToString:@"FACEBOOK"]) {
                        [self.providers addObject:[[FUIFacebookAuth alloc] init]];
                    }
                }
            }
            
            if (@available(iOS 13.0, *)) {
                if ([provider isEqualToString:@"apple.com"]) {
                    [self.providers addObject:[FUIOAuth appleAuthProvider]];
                }
            }

            if ([provider isEqualToString:@"EMAIL"]) {
                [self.providers addObject:[[FUIEmailAuth alloc] init]];
            }
        }

        self.authUI.providers = self.providers;
    }
}

- (void)signInAnonymous {

    if (!self.anonymous) {
        return;
    }

    FIRUser *user = [[FIRAuth auth] currentUser];

    if (user == nil) {

        [[FIRAuth auth] signInAnonymouslyWithCompletion:^(FIRAuthDataResult * _Nullable authResult, NSError * _Nullable error) {
            if ([authResult user] != nil) {
                [self raiseEventForUser:[authResult user]];
            } else if (error != nil) {
                NSDictionary *data = nil;
                
                if (error.localizedFailureReason != nil && error.localizedDescription != nil) {
                    data = @{
                             @"code" : [error localizedFailureReason],
                             @"message" : [error localizedDescription]
                             };
                } else {
                    
                    data = @{
                             @"code" : @-1,
                             @"message" : @"Unknown failure reason"
                             };
                }
                
                [self raiseEvent:@"signinfailure" withData:data];
            }
        }];
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

            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"getToken error %@", [exception reason]);
        @throw exception;
    }
}

- (void)getCurrentUser:(CDVInvokedUrlCommand *)command {

    @try {
        FIRUser *user = [[FIRAuth auth] currentUser];

        if (user != nil) {

            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self formatUser:user]];

            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        } else {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self formatEmptyUser]];

            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"getCurrentUser error %@", [exception reason]);
        @throw exception;
    }
}

- (void)signIn:(CDVInvokedUrlCommand *)command {

    self.signInCallbackId = command.callbackId;

    @try {
        FIRUser *user = [[FIRAuth auth] currentUser];

        if (user != nil && ![user isAnonymous]) {
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

- (void)authUI:(FUIAuth *)authUI didSignInWithAuthDataResult:(nullable FIRAuthDataResult *)authDataResult
         error:(nullable NSError *)error {

    NSDictionary *data = nil;

    if (error == nil) {
        [self raiseEventForUser:authDataResult.user];
    } else {

        if (error.code == FUIAuthErrorCodeUserCancelledSignIn) {
            
            [self signInAnonymous];
            [self raiseEvent:@"signinaborted" withData:data];
            
            return;
        }

        if (error.code == FUIAuthErrorCodeMergeConflict) {
            FIRAuthCredential *authCredential = [error.userInfo valueForKey:FUIAuthCredentialKey];
            
            if (authCredential != nil) {
                [[FIRAuth auth] signInAndRetrieveDataWithCredential:authCredential completion:^(FIRAuthDataResult * _Nullable authResult, NSError * _Nullable error) {
                    if ([authResult user] != nil) {
                        [self raiseEventForUser:[authResult user]];
                    } else if (error != nil) {
                        NSDictionary *data = nil;
                        
                        if (error.localizedFailureReason != nil && error.localizedDescription != nil) {
                            data = @{
                                     @"code" : [error localizedFailureReason],
                                     @"message" : [error localizedDescription]
                                     };
                        } else {
                            
                            data = @{
                                     @"code" : @-1,
                                     @"message" : @"Unknown failure reason"
                                     };
                        }
                        
                        [self raiseEvent:@"signinfailure" withData:data];
                    }
                }];
            }
        } else {
            if (error.localizedFailureReason != nil && error.localizedDescription != nil) {
                data = @{
                         @"code" : [error localizedFailureReason],
                         @"message" : [error localizedDescription]
                         };
            } else {
                
                data = @{
                         @"code" : @1,
                         @"message" : @"Unknown failure reason"
                         };
            }
            
            [self raiseEvent:@"signinfailure" withData:data];
        }
    }
}

- (void)signInAnonymously:(CDVInvokedUrlCommand *)command {
    @try {
        [self signInAnonymous];
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
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

        self.signInCallbackId = command.callbackId;

        if ([self.authUI signOutWithError:nil]) {
            [self raiseEvent:@"signoutsuccess" withData:nil];
            [self signInAnonymous];
        } else {
            [self raiseEvent:@"signoutfailure" withData:nil];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
        @throw exception;
    }
}

- (void)deleteUser:(CDVInvokedUrlCommand *)command {

    @try {
        
        self.signInCallbackId = command.callbackId;

        FIRUser *user = [[FIRAuth auth] currentUser];
        
        [user deleteWithCompletion:^(NSError * _Nullable error) {
            if (error) {
                
                NSDictionary *data = @{
                                        @"code" : @1,
                                        @"message" : @"This operation requires recent authentication. Please log out and back in and try again."
                                    };
                
                [self raiseEvent:@"deleteuserfailure" withData:data];
            } else {
                [self raiseEvent:@"deleteusersuccess" withData:nil];
                [self signInAnonymous];
            }
        }];
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
        @throw exception;
    }
}

- (void)sendEmailVerification:(CDVInvokedUrlCommand *)command {

    @try {
        
        self.signInCallbackId = command.callbackId;

        FIRUser *user = [[FIRAuth auth] currentUser];
        
        [user sendEmailVerificationWithCompletion:^(NSError * _Nullable error) {
            if (error) {
                [self raiseEvent:@"emailverificationnotsent" withData:nil];
            } else {
                [self raiseEvent:@"emailverificationsent" withData:nil];
                [self signInAnonymous];
            }
        }];
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
        @throw exception;
    }
}

- (void)reloadUser:(CDVInvokedUrlCommand *)command {

    @try {
        
        self.signInCallbackId = command.callbackId;

        FIRUser *user = [[FIRAuth auth] currentUser];
        
        [user reloadWithCompletion:^(NSError * _Nullable error) {
            FIRUser *user = [[FIRAuth auth] currentUser];
            
            [self raiseEventForUser:user];
        }];
    }
    @catch (NSException *exception) {
        NSLog(@"SignOut error %@", [exception reason]);
        @throw exception;
    }
}

- (void)raiseEventForUser:(FIRUser *)user {

    NSDictionary *result = [self formatUser:user];

    [self raiseEvent:@"signinsuccess" withData:result];
}

- (NSDictionary *)formatUser:(FIRUser *)user {

    NSDictionary *result;

    NSNumber *isEmailVerified;

    NSNumber *newUser;

    self.anonymous = [user isAnonymous];

    if ([user isEmailVerified]) {
        isEmailVerified = @YES;
    } else {
        isEmailVerified = @NO;
    }

    FIRUserMetadata *metadata = [user metadata];

    NSDate *lastSignInDate = [metadata lastSignInDate];
    NSDate *creationDate = [metadata creationDate];

    if (!self.anonymous && [lastSignInDate compare:creationDate] == NSOrderedSame) {
        newUser = @YES;
    } else {
        newUser = @NO;
    }

    if ([user photoURL] != nil) {
         result = @{@"email" : [self emptyIfNull:[user email]],
                    @"emailVerified" : isEmailVerified,
                    @"name" : [self emptyIfNull:[user displayName]],
                    @"id" : [self emptyIfNull:[user uid]],
                    @"photoUrl" : [[user photoURL] absoluteString],
                    @"newUser" : newUser
                    };
    } else {
        result = @{@"email" : [self emptyIfNull:[user email]],
                   @"emailVerified" : isEmailVerified,
                   @"name" : [self emptyIfNull:[user displayName]],
                   @"id" : [self emptyIfNull:[user uid]],
                   @"newUser" : newUser
                   };
    }

    return result;
}

- (NSDictionary *)formatEmptyUser {

    NSDictionary *result;

    result = @{@"email" : (id)[NSNull null],
               @"emailVerified" : @NO,
               @"name" : (id)[NSNull null],
               @"id" : (id)[NSNull null],
               @"newUser" : @NO
               };

    return result;
}

- (NSString *)emptyIfNull:(NSString *)value {
    if (value == nil) {
        return (id)[NSNull null];
    }

    return value;
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
