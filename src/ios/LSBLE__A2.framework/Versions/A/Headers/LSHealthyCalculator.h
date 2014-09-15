//
//  LHealthyCalculator.h
//  LifesenseA2
//
//  Created by Lifense on 13-1-13.
//  Copyright (c) 2013年 Lifense. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BodyInformation;
@class WeightData;

@interface LSHealthyCalculator : NSObject

typedef enum
{
    BodyInformationSexMale = 1,
    BodyInformationSexFemale = 2,
}BodyInformationSex;


/**
 *  Calculator Body Information
 *
 *  @param height Height unit:m
 *  @param weightdata WeightData
 *  @param age    age
 *  @param sex    sex 1:male 2:female
 *
 *  @return BodyInformation item
 */
+ (BodyInformation *)bodyInformationWithHeight:(double)Height weight:(WeightData *)weightdata age:(int)age sex:(BodyInformationSex)sex;

@end
