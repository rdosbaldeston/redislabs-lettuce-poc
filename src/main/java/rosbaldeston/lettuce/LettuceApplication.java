//
// Copyright (c) 2020 Resonate Group Ltd.  All Rights Reserved.
//

package rosbaldeston.lettuce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Encapsulates the application's main entry point.
 */
// CSOFF: HideUtilityClassConstructor
@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication
public class LettuceApplication
{
    /**
     * The the application's main entry point.
     *
     * @param args
     *            the application's arguments
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(LettuceApplication.class, args);
    }
}
